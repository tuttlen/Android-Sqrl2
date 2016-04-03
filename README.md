# Android-Sqrl2


This is a fork of geir54 work with a lot of additions to the URL for preliminary development. Some of these items are not expected to put into a working version. The following are some of the added enhancements.

* Added bluetooth for Sqrl verification
* Added bluetooth for Exporting Keys (see Enhancement Note)
* Fixed some items and added Unit Tests
* Import SQRLdata via Google Drive
  - Still have trouble importing identities from GRC client

The following are critical things that need to be implemented

* Validation of URL (Bluetooth and Web) so that MITM is hampered
[x] Change Base64 encoding to URL [rfc4648] (http://tools.ietf.org/html/rfc4648)
* Build in SCrypt and AES libraries to properly decode the rest of the packet
* 
