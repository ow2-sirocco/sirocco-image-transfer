====

    SIROCCO
    Copyright (C) 2011 France Telecom
    Contact: sirocco@ow2.org

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
    USA

     $Id$

====

When launching the ImageTransferServer, specify on the run command line:

1) the maximum simultaneous image transfers using the same HTTP route
by passing, as a JVM argument, the property -DhttpRouteConxMax=N
(with N>1). If this property is not set, such connections will be
limited to 2.

2) the IP and port of the host that will run the REST ImageTransferServer
by passing, as program arguments, "hostIP" followed by "port" values.
If these arguments are not set, the default will be "localhost" as host
value and "8080" as port value.

When launching the ImageTransferClient:

if you provided the "hostIP" and "port" program arguments to the ImageTransferServer,
instantiate a new ImageTransferClient class with the
"http://hostIP:port/sirocco/imagetransfer" string URL as the constructor argument.
Otherwise, provides the "http://localhost:8080/sirocco/imagetransfer" string URL.

