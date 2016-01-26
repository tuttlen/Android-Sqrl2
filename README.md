# Android-Sqrl2
Fork of geir54 with some addition

This is a fork of geir54 work with a lot of additions to the URL for preliminary development. Some of these items are not expected to put into a working version. The following are some of the added enhancements.

*Added bluetooth for Sqrl verification
*Added bluetooth for Exporting Keys (see Enhancement Note)
*Fixed some items and added Unit Tests

The following are critical things that need to be implemented

*Validation of URL (Bluetooth and Web) so that MITM is hampered
*S4 implementation of key storage
*Change Base64 encoding to URL rfc4648 | http://tools.ietf.org/html/rfc4648
*Fix TODO's 
*Cleanup code

Issues

*The layout is a complete disaster
*Need to make the Bluetooth list come up without words NOT in white so user can see it
*Sometimes login screen does not work
*Fix Intents to properly go between screens

