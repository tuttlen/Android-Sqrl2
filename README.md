# Android-Sqrl2

## Currently not functional

This is a fork of geir54 work with a lot of additions to the URL for preliminary development. Some of these items are not expected to put into a working version. The following are some of the added enhancements.

* Added bluetooth for Sqrl verification
* Added bluetooth for Exporting Keys (see Enhancement Note)
* Fixed some items and added Unit Tests
* Import SQRLdata via Google Drive
  - The specification for SQRL data seems to be in flux. I am unclear on how to decipher some of the packet otherwise I am close       to being able to import Steve's SQRL identity files.

The following are critical things that need to be implemented

* Validation of URL (Bluetooth and Web) so that MITM is hampered
* S4 implementation of key storage startd project at (https://github.com/tuttlen/SqrlData.git)
* Change Base64 encoding to URL [rfc4648] (http://tools.ietf.org/html/rfc4648)
* Fix TODO's 
* Cleanup code
* Build in SCrypt and AES libraries to properly decode the rest of the packet
* 
# Issues

* UI work
* Need to make the Bluetooth list come up without words NOT in white so user can see it
* Sometimes login screen does not work
* Fix Intents to properly go between screens
* Correctly unencrypt and authenticate the rest of the SQRL packet


