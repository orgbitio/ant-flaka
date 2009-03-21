/*
 * Copyright (c) 2009 Haefelinger IT 
 *
 * Licensed  under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required  by  applicable  law  or  agreed  to in writing, 
 * software distributed under the License is distributed on an "AS 
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied.
 
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package net.haefelingerit.flaka;

public class HttpStatusText
{
  final static public String explain(int status)
  {
    switch (status)
    {
      case 100:
        return "100 Continue: The client SHOULD continue with its request. This interim response is used to inform the client that the initial part of the request has been received and has not yet been rejected by the server.";

      case 101:
        return "101 Switching Protocols: The server understands and is willing to comply with the client's request, via the Upgrade message header field (section 14.42), for a change in the application protocol being used on this connection.";

      case 200:
        return "200 OK: The request has succeeded.";

      case 201:
        return "201 Created: The request has been fulfilled and resulted in a new resource being created.";

      case 202:
        return "202 Accepted: The request has been accepted for processing, but the processing has not been completed.";

      case 203:
        return "203 Non-Authoritative Information: The returned metainformation in the entity-header is not the definitive set as available from the origin server, but is gathered from a file or a third-party copy.";

      case 204:
        return "204 No Content: The server has fulfilled the request but does not need to return an entity-body, and might want to return updated metainformation.";

      case 205:
        return "205 Reset Content: The server has fulfilled the request and the user agent SHOULD reset the document view which caused the request to be sent.";

      case 206:
        return "206 Partial Content: The server has fulfilled the partial GET request for the resource. The request MUST have included a Range header field (section 14.35) indicating the desired range, and MAY have included an If-Range header field (section 14.27) to make the request conditional.";

      case 300:
        return "300 Multiple Choices: The requested resource corresponds to any one of a set of representations, each with its own specific location, and agent- driven negotiation information (section 12) is being provided so that the user (or user agent) can select a preferred representation and redirect its request to that location.";

      case 301:
        return "301 Moved Permanently: The requested resource has been assigned a new permanent URI and any future references to this resource SHOULD use one of the returned URIs.";

      case 302:
        return "302 Found: The requested resource resides temporarily under a different URI.";

      case 303:
        return "303 See Other: The response to the request can be found under a different URI and SHOULD be retrieved using a GET method on that resource.";

      case 304:
        return "304 Not Modified: If the client has performed a conditional GET request and access is allowed, but the document has not been modified, the server SHOULD respond with this status code.";

      case 305:
        return "305 Use Proxy: The requested resource MUST be accessed through the proxy given by the Location field. The Location field gives the URI of the proxy. The recipient is expected to repeat this single request via the proxy.";

      case 306:
        return "306 (Unused): The 306 status code was used in a previous version of the specification, is no longer used, and the code is reserved.";

      case 307:
        return "307 Temporary Redirect: The requested resource resides temporarily under a different URI.";
      case 401:
        return "401 Unauthorized: The request requires user authentication. The response MUST include a WWW-Authenticate header field (section 14.47) containing a challenge applicable to the requested resource.";
      case 402:
        return "402 Payment Required: This code is reserved for future use.";
      case 403:
        return "403 Forbidden: The server understood the request, but is refusing to fulfill it. Authorization will not help and the request SHOULD NOT be repeated.";
      case 404:
        return "404 Not Found: The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.";
      case 405:
        return "405 Method Not Allowed: The method specified in the Request-Line is not allowed for the resource identified by the Request-URI.";
      case 406:
        return "406 Not Acceptable: The resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request.";

      case 407:
        return "407 Proxy Authentication Required: This code is similar to 401 (Unauthorized), but indicates that the client must first authenticate itself with the proxy.";

      case 408:
        return "408 Request Timeout: The client did not produce a request within the time that the server was prepared to wait.";

      case 409:
        return "409 Conflict: The request could not be completed due to a conflict with the current state of the resource.";

      case 410:
        return "410 Gone: The requested resource is no longer available at the server and no forwarding address is known. This condition is expected to be considered permanent.";

      case 411:
        return "411 Length Required: The server refuses to accept the request without a defined Content- Length.";

      case 412:
        return "412 Precondition Failed: The precondition given in one or more of the request-header fields evaluated to false when it was tested on the server.";

      case 413:
        return "413 Request Entity Too Large: The server is refusing to process a request because the request entity is larger than the server is willing or able to process.";

      case 414:
        return "414 Request-URI Too Long: The server is refusing to service the request because the Request-URI is longer than the server is willing to interpret.";

      case 415:
        return "415 Unsupported Media Type: The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method.";

      case 416:
        return "416 Requested Range Not Satisfiable: A server SHOULD return a response with this status code if a request included a Range request-header field (section 14.35), and none of the range-specifier values in this field overlap the current extent of the selected resource, and the request did not include an If-Range request-header field.";

      case 417:
        return "417 Expectation Failed: The expectation given in an Expect request-header field (see section 14.20) could not be met by this server, or, if the server is a proxy, the server has unambiguous evidence that the request could not be met by the next-hop server.";

      case 500:
        return "500 Internal Server Error: The server encountered an unexpected condition which prevented it from fulfilling the request.";

      case 501:
        return "501 Not Implemented: The server does not support the functionality required to fulfill the request. This is the appropriate response when the server does not recognize the request method and is not capable of supporting it for any resource.";

      case 502:
        return "502 Bad Gateway: The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed in attempting to fulfill the request.";
      case 503:
        return "503 Service Unavailable: The server is currently unable to handle the request due to a temporary overloading or maintenance of the server.";

      case 504:
        return "504 Gateway Timeout: The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI (e.g. HTTP, FTP, LDAP) or some other auxiliary server (e.g. DNS) it needed to access in attempting to complete the request.";

      case 505:
        return "505 HTTP Version Not Supported: The server does not support, or refuses to support, the HTTP protocol version that was used in the request message.";

      default:
        return status + ": not a status according to RFC 2616, http://www.w3.org/Protocols/rfc2616";
    }
  }
}
