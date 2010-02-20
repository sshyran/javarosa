import struct
from datetime import datetime

class EndOfStream (Exception):
  def __init__ (self, bytes):
    self.bytes = bytes
    
def stream (data):
  for c in data:
    yield c

def read_bytes (dstr, n):
  bytes = []
  try:
    for i in range(0, n):
      bytes.append(dstr.next())
  except StopIteration:
    raise EndOfStream(bytes)
  
  return ''.join(bytes)

def get (typed_val):
  return typed_val[1]
    
def read_int (dstr, require_pos=False):
  (nb, c) = ([], None)
  try:
    while c == None or ord(c) >= 128:
      c = dstr.next()
      nb.append(c)
  except StopIteration:
    raise EndOfStream(nb)

  nv = [ord(c) % 128 for c in nb]
  if nv[0] >= 64:
    nv[0] -= 128
  val = reduce(lambda x, y: 128 * x + y, nv, 0)
  
  if val < 0 and require_pos:
    raise ValueError
  return ('int', val)

def read_string (dstr):
  lb = [ord(i) for i in read_bytes(dstr, 2)]
  ln = 256 * lb[0] + lb[1]

  try:
    val = read_bytes(dstr, ln)
    return ('str', val)
  except EndOfStream, eos:
    raise EndOfStream(lb + eos.bytes)

def read_bool (dstr):
  try:
    b = ord(dstr.next())
  except StopIteration:
    raise EndOfStream([])

  if b != 0 and b != 1:
    raise ValueError
  return ('bool', b == 1)

def read_float (dstr):
  raw = read_bytes(dstr, 8)
  val = struct.unpack('!d', raw)[0]
  return ('dbl', val)
  
def read_date (dstr):
  val = datetime.utcfromtimestamp(get(read_int(dstr)) / 1000.)
  return ('date', val)

def read_binary (dstr):
  return ('bytes', read_bytes(dstr, get(read_int(dstr))))
  
#todo: the parse functions for compound objects don't retain the whole bytestream during unexpected end-of-stream,
#nor do they keep around the (partial) data that led to value errors

def read_null (dstr, type):
  if get(read_bool(dstr)):
    return parse(dstr, type)
  else:
    return (fix(type[0]), None)
    
def fix (type_name):
  if type_name in ['null', 'tagged']:
    return 'generic'
  elif type_name == 'listp':
    return 'list'
  elif type_name == 'mapp':
    return 'map'
  else:
    return type_name
    
def read_list (dstr, type):
  return read_list_helper(dstr, lambda dstr: parse(dstr, type))

def read_list_helper(dstr, get_elem):
  v = []
  n = get(read_int(dstr))
  for i in range(0, n):
    v.append(get_elem(dstr))
  return ('list', v)
  
def read_map (dstr, keytype, elemtype):
  return read_map_helper(dstr, keytype, lambda dstr: parse(dstr, elemtype))

def read_map_helper (dstr, keytype, get_elem):
  m = {}
  n = get(read_int(dstr))
  for i in range(0, n):
    k = parse(dstr, keytype)
    v = get_elem(dstr)
    m[k] = v
  return ('map', m)
  
def read_tagged (dstr):
  tag = read_bytes(dstr, 4)
  if tag in type_tags:
    type = type_tags[tag]
    
    if type == 'wrapper':
      raise ValueError("don't support wrapper tags currently")
    elif type == 'generic':
      raise ValueError("don't know how to handle generic")
    
    return parse(dstr, [type])
  else:
    raise ValueError("don't know tag [%s]" % repr(tag))
  
def read_list_poly (dstr):
  return read_list_helper(dstr, lambda dstr: read_tagged(dstr))
  
def read_map_poly (dstr, keytype):
  return read_map_helper(dstr, keytype, lambda dstr: read_tagged(dstr))
  
def get_parse_func (name):
  if name in builtin_types:
    return builtin_types[name]
  elif name.startswith('obj:'):
    objname = name[4:]
    if objname not in custom_types:
      raise ValueError('unknown type [%s]' % objname)
      
    return lambda *args: (name, custom_types[objname](*args))
  
def parse (dstr, type):
  name = type[0]
  args = type[1:]
  
  return get_parse_func(name)(*([dstr] + args)) 
  
def parse_data (dstr, template):
  val = get(parse_template(dstr, template))
  if len(val) > 1:
    raise ValueError('cannot be sequence')
  return val[0]
  
def parse_template (dstr, template):
  types = parse_type_template(template)
  data = []
  
  for type in types:
    data.append(parse(dstr, type))
  
  return ('seq', tuple(data))
  
def parse_custom (template):
  return lambda dstr: get(parse_template(dstr, template))
  
def parse_type_template (template):
  return parse_token_list(template)

def parse_token_list (toklist):
  tokens = []

  depth = 0
  tok_start = 0
  for tok_end in range(0, len(toklist) + 1):
    new_token = False
    if tok_end == len(toklist):
      if depth == 0:
        new_token = True
      else:
        raise ValueError('unbalanced parens')
    elif toklist[tok_end] == ',' and depth == 0:
      new_token = True
    elif toklist[tok_end] == '(':
      depth += 1
    elif toklist[tok_end] == ')':
      depth -= 1
      if depth < 0:
        raise ValueError('unbalanced parens')

    if new_token:
      tokens.append(parse_token(toklist[tok_start:tok_end]))
      tok_start = tok_end + 1    

  return tokens

def parse_token (tokstr):
  token = []

  if '(' in tokstr and tokstr[-1] != ')':
    raise ValueError('extra crap after close paren')
    
  if '(' in tokstr:
    name = tokstr[:tokstr.find('(')]
    args = parse_token_list(tokstr[tokstr.find('(')+1:-1])
  else:
    name = tokstr
    args = []
  
  if len(name) == 0:
    raise ValueError('empty token name')
  
  validate_token(name, args)
  
  token.append(name)
  token.extend(args)    
  return token

def validate_token (name, args):
  allowed = {'int': 0, 'bool': 0, 'dbl': 0, 'str': 0, 'date': 0, 'obj:': 0, 'null': 1, 'tagged': 0, 'list': 1, 'listp': 0, 'map': 2, 'mapp': 1, 'bytes': 0}
  
  if name == 'obj:':
    raise ValueError('custom object not specified')
  
  if name not in allowed:
    if name.startswith('obj:'):
      name = 'obj:'
    else:
      raise ValueError('unrecognized type [%s]' % name)

  if len(args) != allowed[name]:
    raise ValueError('wrong number of args for [%s]' % name)


builtin_types = {
  'int': read_int,
  'bool': read_bool,
  'dbl': read_float,
  'str': read_string,
  'date': read_date,
  'null': read_null,
  'tagged': read_tagged,
  'list': read_list,
  'listp': read_list_poly,
  'map': read_map,
  'mapp': read_map_poly,
  'bytes': read_binary
}


# relies on stream containing ONLY data for the record
def _parse_property (dstr):
  return (('str', ''.join(list(dstr))),)

def _parse_tree_child (dstr):
  if get(read_bool(dstr)):
    val = parse_data(dstr, 'obj:treeelem')
  else:
    val = read_tagged(dstr) # if this happens, which it almost certainly won't, we almost certainly won't have the prototype registered
  return (val,)
    
def _parse_xpath_num_lit (dstr):
  if get(read_bool(dstr)):
    val = read_float(dstr)
  else:
    val = read_int(dstr)
  return (val,)

def _parse_xpath_path (dstr):
  type = read_int(dstr)
  filtexpr = parse_data(dstr, 'obj:xpath-expr-filt') if get(type) == 2 else None
  steps = parse_data(dstr, 'list(obj:xpath-step)')
  return (type, filtexpr, steps) if filtexpr != None else (type, steps)

def _parse_xpath_step (dstr):
  axis = read_int(dstr)
  test = read_int(dstr)
  if get(test) == 0:
    detail = parse_data(dstr, 'obj:qname')
  elif get(test) == 2:
    detail = read_string(dstr)
  elif get(test) == 6:
    detail = parse_data(dstr, 'null(str)')
  else:
    detail = None
  preds = parse_data(dstr, 'listp')

  return (axis, test, detail, preds) if detail != None else (axis, test, preds)
  
custom_types = {
  'rmsinfo': parse_custom('int,int,int'),
  'recloc': parse_custom('int,int'),
  'user': parse_custom('str,str,str,int,int,str,bool,map(str,str)'),
  'case': parse_custom('str,str,str,str,bool,null(date),int,mapp(str)'),
  'patref': parse_custom('str,date,date,str,str,int,bool'),
  'formdef': parse_custom('int,str,null(str),listp,obj:forminst,null(obj:loclzr),list(obj:condition),list(obj:recalc),listp'),
  'qdef': parse_custom('int,null(str),null(str),null(str),null(str),null(str),null(str),null(str),int,list(obj:selchoice),null(tagged)'),
  'selchoice': parse_custom('bool,str,str'),
  'gdef': parse_custom('int,tagged,null(str),null(str),null(str),null(str),bool,listp,bool,tagged'), #null(tagged)'),
  'loclzr': parse_custom('bool,bool,map(str,listp),list(str),null(str),null(str)'),
  'resfiledatasrc': parse_custom('str'),
  'localedatasrc': parse_custom('map(str,str)'),
  'condition': parse_custom('tagged,obj:treeref,list(obj:treeref),int,int'),
  'recalc': parse_custom('tagged,obj:treeref,list(obj:treeref)'),
  'treeref': parse_custom('int,list(str),list(int)'),
  'forminst': parse_custom('int,int,null(str),null(str),null(date),map(str,str),obj:treeelem'),
#  'forminst-compact': ...,   oh boy...
  'treeelem': parse_custom('str,int,bool,null(tagged),null(list(obj:treechildhack)),int,bool,bool,bool,bool,bool,null(obj:constraint),str,str,list(str)'),
  'treechildhack': _parse_tree_child,
  'intdata': parse_custom('int'),
  'booldata': parse_custom('bool'),
  'strdata': parse_custom('str'),
  'selonedata': parse_custom('obj:sel'),
  'selmultidata': parse_custom('list(obj:sel)'),
  'sel': parse_custom('str,int'),
  'floatdata': parse_custom('dbl'),
  'datedata': parse_custom('date'),
  'datetimedata': parse_custom('date'),
  'timedata': parse_custom('date'),
  'constraint': parse_custom('tagged,str'),
  'xpathcond': parse_custom('tagged'),
  'xpathref': parse_custom('str,obj:treeref'),
  'xpath-expr-arith': parse_custom('int,tagged,tagged'),
  'xpath-expr-bool': parse_custom('int,tagged,tagged'),
  'xpath-expr-cmp': parse_custom('int,tagged,tagged'),
  'xpath-expr-eq': parse_custom('bool,tagged,tagged'),
  'xpath-expr-filt': parse_custom('tagged,listp'),
  'xpath-expr-func': parse_custom('obj:qname,listp'),
  'xpath-expr-numlit': _parse_xpath_num_lit,
  'xpath-expr-numneg': parse_custom('tagged'),
  'xpath-expr-path': _parse_xpath_path,
  'xpath-expr-strlit': parse_custom('str'),
  'xpath-expr-union': parse_custom('tagged,tagged'),
  'xpath-expr-varref': parse_custom('obj:qname'),
  'xpath-step': _parse_xpath_step,
  'qname': parse_custom('null(str),str'),
  'property': _parse_property,
  'txmsg': parse_custom('tagged'),
  'simplehttptxmsg': parse_custom('str,int,str,int,str,date,date,int,int,str,int,str,bytes'),
  'logentry': parse_custom('date,str,str'),
  'cc-recd-forms-mapping': parse_custom('list(int),map(int,int)')
}
  
type_tags = {
  '\xff\xff\xff\xff': 'wrapper',
  '\xe5\xe9\xb5\x92': 'generic', #object -- should never be encountered
  '\x7c\xa1\x6f\xdb': 'int',
  '\x8a\xc5\x87\x0b': 'int', #long
  '\xb5\xdc\x2e\x41': 'int', #short
  '\x03\x3e\xb3\x91': 'int', #byte
  '\x58\x4b\x12\x84': 'char',
  '\xe4\xf9\xf9\xae': 'bool',
  '\xc9\x83\xee\x7b': 'dbl', #float
  '\x8e\xa8\x96\x89': 'dbl',
  '\x42\xc2\x5b\xe3': 'str',
  '\xc5\x1d\xfd\xa6': 'date',
  '\x27\x51\x2e\xc9': 'obj:qdef',
  '\xb3\xc4\x9b\xbd': 'obj:gdef',
  '\x68\xc2\xaf\xad': 'obj:intdata',
  '\x8f\x4b\x45\xfe': 'obj:booldata',
  '\xed\xce\xd1\xce': 'obj:geodata',
  '\x02\x6f\x56\x15': 'obj:strdata',
  '\x29\xd7\x1a\x40': 'obj:selonedata',
  '\xf7\x30\xcc\x7d': 'obj:selmultidata',
  '\x4e\x52\xe2\x15': 'obj:floatdata',
  '\x51\x0e\x1e\x6e': 'obj:datedata',
  '\x6f\x87\x88\xa7': 'obj:datetimedata',
  '\x68\x4e\x4e\x2e': 'obj:timedata',
  '\x2b\xf7\x1a\xcb': 'obj:ptrdata',
  '\xec\xa8\xec\xde': 'obj:multiptrdata',
  '\xef\x74\x56\x54': 'obj:basicdataptr',
  '\xf3\x06\x34\x28': 'obj:xpath-expr-arith',
  '\xf6\xe4\xb9\xaf': 'obj:xpath-expr-bool',
  '\x91\x2e\xfc\xee': 'obj:xpath-expr-cmp',
  '\x65\x71\x6e\x97': 'obj:xpath-expr-eq',
  '\xe7\x68\xb3\x6d': 'obj:xpath-expr-filt',
  '\x67\x44\xc2\x7e': 'obj:xpath-expr-func',
  '\x17\xe0\x31\x27': 'obj:xpath-expr-numlit',
  '\x35\x60\xa2\x3b': 'obj:xpath-expr-numneg',
  '\xfc\x87\x51\x53': 'obj:xpath-expr-path',
  '\xef\x45\x98\x8f': 'obj:xpath-expr-strlit',
  '\xff\x82\x5b\x62': 'obj:xpath-expr-union',
  '\xf9\x4b\xf7\xa8': 'obj:xpath-expr-varref',
  '\x5c\x57\xbb\x5e': 'obj:xpathref',
  '\x5e\x88\x11\xfe': 'obj:xpathcond',
  '\xf4\xaa\xb2\xe9': 'obj:resfiledatasrc',
  '\xf6\xc7\x83\x5c': 'obj:localedatasrc',
  '\x27\x53\xac\x23': 'obj:simplehttptxmsg',
  '\x01\x12\x89\x43': 'obj:smstxmsg',
  '\x21\x71\xd6\x5d': 'obj:binsmstxmsg',
 # '\xed\x09\xe3\x8e': 'obj:forminst', #unused i think
 # '\xfb\x2c\xa2\x76': 'obj:txmsgserwrapper' #unused i think
}

def print_data (data, indent=0, suppress_start=False, suppress_end=False):
  return print_data_helper(data, indent, suppress_start) + ('\n' if not suppress_end else '')

def print_data_helper (data, indent, suppress_indent=False):
  buf = ''
  (type, val) = data

  IND = '  ' * indent
  if not suppress_indent:
    buf += IND
  
  if type == 'int':
    buf += 'i %d' % val if val != None else 'i <null>'
  elif type == 'dbl':
    buf += 'f %f' % val if val != None else 'f <null>'
  elif type == 'bool':
    buf += 'b %s' % ('true' if val else 'false') if val != None else 'b <null>'
  elif type == 'str' or type == 'bytes':
    buf += 's %s' % repr(val) if val != None else 's <null>'
  elif type == 'date':
    if val != None:
      sdt = val.strftime('%Y-%m-%d %H:%M:%S')
      buf += 'dt %s' % sdt
    else:
      buf += 'dt <null>'
  elif type == 'generic':
    buf += '? <null>'
  elif type == 'seq':
    buf += 'seq #%d (\n' % len(val)
    for i in range(0, len(val)):
      buf += print_data_helper(val[i], indent + 1)
      buf += ',\n' if i < len(val) - 1 else '\n'
    buf += IND + ')'
  elif type.startswith('obj:'):
    if val != None:
      buf += type + ' (\n'
      for i in range(0, len(val)):
        buf += print_data_helper(val[i], indent + 1)
        buf += ',\n' if i < len(val) - 1 else '\n'
      buf += IND + ')'
    else:
      buf += type + ' <null>'
  elif type == 'list':
    if val != None:
      buf += 'list #%d [' % len(val)
      if len(val) > 0:
        buf += '\n'
        for i in range(0, len(val)):
          buf += print_data_helper(val[i], indent + 1)
          buf += ',\n' if i < len(val) - 1 else '\n'
        buf += IND
      buf += ']'
    else:
      buf += 'list <null>'
  elif type == 'map':
    if val != None:
      buf += 'map #%d {' % len(val)
      if len(val) > 0:
        buf += '\n'
        for (i, (k, v)) in enumerate(val.iteritems()):
          buf += print_data_helper(k, indent + 1)
          buf += ' => '
          buf += print_data_helper(v, indent + 1, True)
          buf += ',\n' if i < len(val) - 1 else '\n'
        buf += IND
      buf += '}'
    else:
      buf += 'map <null>'

  return buf

  
  

  
  
  
def hex_to_stream (hexstr):
  return stream(''.join([chr(int(c, 16)) for c in hexstr.split()]))
