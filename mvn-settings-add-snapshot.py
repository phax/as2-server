#!/usr/bin/env python
#
# The FreeBSD Copyright
# Copyright 1994-2008 The FreeBSD Project. All rights reserved.
# Copyright (C) 2013-2016 Philip Helger philip[at]helger[dot]com
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#
#    1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
#
#    2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT ``AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
# PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
# PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# The views and conclusions contained in the software and documentation
# are those of the authors and should not be interpreted as representing
# official policies, either expressed or implied, of the FreeBSD Project.
#

import sys
import os
import os.path
import xml.dom.minidom

# Source and credits:
# https://gist.github.com/neothemachine/4060735

if os.environ["TRAVIS_SECURE_ENV_VARS"] == "false":
  print "no secure env vars available, skipping deployment"
  sys.exit()

homedir = os.path.expanduser("~")

m2 = xml.dom.minidom.parse(homedir + '/.m2/settings.xml')
settings = m2.getElementsByTagName("settings")[0]

serversNodes = settings.getElementsByTagName("servers")
if not serversNodes:
  serversNode = m2.createElement("servers")
  settings.appendChild(serversNode)
else:
  serversNode = serversNodes[0]
  
sonatypeServerNode = m2.createElement("server")

sonatypeServerId = m2.createElement("id")
# See the name "ossrh" in the ph-parent-pom project
# Original name was "sonatype-nexus-snapshots"
idNode = m2.createTextNode("ossrh")
sonatypeServerId.appendChild(idNode)
sonatypeServerNode.appendChild(sonatypeServerId)

sonatypeServerUser = m2.createElement("username")
userNode = m2.createTextNode(os.environ["SONATYPE_USERNAME"])
sonatypeServerUser.appendChild(userNode)
sonatypeServerNode.appendChild(sonatypeServerUser)

sonatypeServerPass = m2.createElement("password")
passNode = m2.createTextNode(os.environ["SONATYPE_PASSWORD"])
sonatypeServerPass.appendChild(passNode)
sonatypeServerNode.appendChild(sonatypeServerPass)

serversNode.appendChild(sonatypeServerNode)
  
m2Str = m2.toxml()
f = open(homedir + '/.m2/snapshot-settings.xml', 'w')
f.write(m2Str)
f.close()
