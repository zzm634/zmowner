This file contains some notes and other protocol details that might help others write better software for these cameras. Most of it was decoded from the camera source code someone posted on github, as well as by capturing network traffic to and from the camera when connecting to it with Zmodo's own Zviewer program.

Luckily, Zviewer can be installed without agreeing to any EULA, and I couldn't find any sort of contract or clause barring reverse engineering. The camera source code was posted by someone else, who so far seems to be anonymous.

# Data stream
The camera responds to commands in a single stream of "chunks" or "packets" or whatever you want to call them. I'll call them packets.  Each packet starts with a 4-byte identifier specifying the type and therefore format of the packet. That's the only common thing to all packets. The rest must be decoded

## Command packets
Command packets start with the hex bytes [0x55, 0x55, 0xAA, 0xAA] followed by a fixed size header and variable sized payload. Commands can be sent to the camera using these packets, and the camera will respond with an echo of the command header, followed by any payload information for the requested command.

Within the command header are two bytes that specify the command type. These are enumerated in 'interfacedef.h' and handled in 'netuser.cpp'. Notable command type values (big-endian) indclude:

- 0x3696: get AES key from camera (CMD_G_VIDEO_KEY)
- 0xA290: start streaming VGA video (CMD_START_VGA)
- 0x0050: start streaming 720p video (CMD_START_720P)
- 0x6690: enable or disable audio streaming (CMD_SET_AUDIOSWITCH)
- 0x6090: get audio encoder params (CMD_G_TALK_SETTING)

The command header is structured as follows

- 4 byte identifier [0x55, 0x55, 0xAA, 0xAA]
- 2 byte payload length (16 bit little-endian integer)
- 4 bytes unknown
- 2 byte command identifier

### Get AES key
Video data is partially encrypted using an AES key stored in the camera, which can be retrieved using this command. It is a 256-bit key that (at least in my camera's example) just happens to consist of ASCII hexadecimal digits. Don't be fooled though, the key is not the hexadecimal number these digits represent, it's the raw byte values.

### Stream video
To start streaming video from the camera, sending the "start video" command is all that is necessary, with no payload. `5555aaaa0000000000000050` for example, would be the "start 720p video stream" command. The camera will echo the response back, then start periodically sending video frames.

### Stream audio
Based on captured traffic, there seems to be two commands issued to the camera: one to get the audio encoder parameters, and one to enable the transmission of audio.

I haven't decoded what the audio encoder parameters returned by the "CMD_G_TALK_SETTING" command mean yet.

Unlike the "start video stream" commands, which have no payload, audio streaming is started by setting a flag using the "set audioswitch" command, with a 32 bit integer payload (1 for on, 0 for off)

## Video Stream Packets
In what seems to be an attempt to disguise the video stream format, the video stream packet identifiers look like AVI chunk headers (00dc, 01dc, etc). However, these are not AVI chunks; the header contains additional data that must be dealt with. Additionally, the P-frames and I-frames are separated and identified with different header values. If naively interpreted as an AVI data stream, it would appear to have two different video streams, when really there is only one.

The video frame header contains additional metadata about the time the frame was recorded, the framerate of the video, whether or not motion was detected, etc. Check out `VideoFrameHandler.java` for the details.

The video stream itself seems to be h264 encoded chunks.

### P-frames
P-frame chunks (complete images) start with "X0dc" where X is the ascii value of the stream index. (i.e., if there are two video streams, P-frames will start with "00dc" and "10dc" respectively). The first 256 bytes of every P-frame are encrypted with AES-CBC and must be decrypted with the camera's AES key. The rest of the chunk data is not encrypted.

### I-frames
I-frame chunks (interstitial images) start with "X1dc" where X identifies the stream the chunks belong to. I-frames are not encrypted.

## Audio Stream Packets
Audio chunks start with "X1wb", where X identifies the stream it belongs to. The header only includes information about the length of the payload and the chunk timestamp. See `AudioFrameHandler.java` for the details.

So far, I have not been successful in properly decoding or re-muxing the audio stream. I believe it is a G.711 audio stream, based on `stAdecAttr.enType = PT_G711A;` in `Audio.cpp`.

It's also likely possible to change the audio encoding settings to a higher quality codec, but again, haven't tried yet.
