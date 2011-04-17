#!/bin/env python


import os
import re
import sys


ws = re.compile(r'-')
f = open("list.txt")
names = f.readlines()
f.close()
for name in names:
   name = name[0:-1]
   newname = ""
   for token in  ws.split(name):
      newname += token[0].upper()
      newname += token[1:]
   cmd  = "cp %s %s" % (name,newname)
   print cmd
   os.system(cmd)
   
