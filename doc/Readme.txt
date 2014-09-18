====
    The FreeBSD Copyright
    Copyright 1994-2008 The FreeBSD Project. All rights reserved.
    Copyright (C) 2013-2014 Philip Helger philip[at]helger[dot]com

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are
    met:

       1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

       2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT ``AS IS'' AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR
    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

    The views and conclusions contained in the software and documentation
    are those of the authors and should not be interpreted as representing
    official policies, either expressed or implied, of the FreeBSD Project.
====

IMPORTANT: 
You must have the proper JCE policy files install with your JRE for encryption to work correctly.
Go to http://java.sun.com/j2se/downloads.html
Select the version of java you use
At the bottom of the page under "Other Downloads", download the JCE unlimited strength jurisdiction 
policy file and follow the directions with it for installation.


To quickly start OpenAS2:

In windows:
- go to the <project> directory
- double-click on .\bin\start-openas2.bat
- type "cert" or "part" at the command prompt to get lists of some useful commands
- type "exit" to shut down the server


To import a public certificate:

#>cert import <x509 alias used in partnerships> <.cer filename>


To import a public certificate and it's private key:

#>cert import <x509 alias> <.p12 filename> <password to access private key in .p12 file>




