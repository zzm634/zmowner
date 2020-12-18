# ZmodOwned

This is an attempt to reverse-engineer the protocol used by Zmodo IP cameras in order to make use of the video and audio feeds. At the moment, it is extremely barebones, and won't work without serious networking and scripting know-how. But... the hard work is done. Upcoming features include:

- Connecting to cameras by IP address
- Automatic retrieval of AES key
- Audio streaming
- Framerate detection
- Packaging multiple video and audio streams
- Conversion of .264 files from Zviewer and other Zmodo DVR software

## Disclaimer

Zmodo probably won't like this, and will attempt to further obfuscate their video streams in new firmwares. Do not update your camera firmware.

## Tested devices

- Zmodo Ding doorbell (now called "Greet")

## Usage

The java executable 'zmodowned.jar' is used to decrypt streams.

### Manual stream decryption

At the moment, the jar expects to see only the encrypted stream data, and does not strip the headers. It also doesn't package the output data in any way. To use it, you have to fiddle with the stream a bit.

1. Retrieve the AES key from the camera

    echo -n -e '\x55\x55\xaa\xaa\x00\x00\x00\x00\x00\x00\x36\x96' | nc -q 1 CAMERA_IP_ADDRESS 8000 | tail -c +13 | head --bytes 32 && echo ""

  1. issue the "get AES key" command ``echo -n -e '\x55\x55\xaa\xaa\x00\x00\x00\x00\x00\x00\x36\x96' | nc -q 1 $CAM_IP 8000``

  2. strip off the response header ``tail -c +13``

  3. strip off the trailing data ``head --bytes 32 && echo ""``

  Note that the returned key may look like a 128-bit hexadecimal key, but they are actually using a 256-bit key, which happens to be made of these ASCII digits. If you get garbled output, they may have switched to using a real 256-bit key of arbitrary data, and you may need to pipe the key into hexdump to see the hexidecimal values of the key.

2. Dump the stream from the camera. It's a live stream, so you'll have to interrupt it on your own to stop recording.

    echo -n -e '\x55\x55\xaa\xaa\x00\x00\x00\x00\x00\x00\x00\x50' | nc CAMERA_IP_ADDRESS 8000 | tail -c +17 > stream.264

  It may be useful to put this command into a script (without the > ``stream.264``) part, if you want to stream live video

3. Feed the encrypted stream into zmodowned.jar

    cat stream.264 | java -jar zmodowned.jar CAMERA_AES_KEY > stream_decrypted.264

4. Feed the decrypted stream into ffmpeg to repackage it as an MP4

    ffmpeg -f h264 -i stream_decrypted.264 -c copy stream_decrypted.mp4

5. enjoy.

You can package all this fun stuff together to dump a live MP4 of your camera

    echo -n -e '\x55\x55\xaa\xaa\x00\x00\x00\x00\x00\x00\x00\x50' | nc CAMERA_IP_ADDRESS 8000 | tail -c +17 | java -jar zmodowned.jar CAMERA_AES_KEY | ffmpeg -re -an -f h264 -i - -c copy stream.mp4

    ffmpeg options:
    -re = realtime stream data
    -an = no audio
    -f h264 = format: raw h264 stream
    -i - = input = std in
    -c copy = copy input streams (no transcoding)

## Integration with DVRs

This project is mostly about decrypting the stream from the cameras. To use the cameras in a DVR system (such as Agent DVR, ZoneMinder, Blue Iris, etc.), you'll need to set up a streaming server to handle the incoming connections from those DVRs. I used https://github.com/aler9/rtsp-simple-server for this.

    echo -n -e '\x55\x55\xaa\xaa\x00\x00\x00\x00\x00\x00\x00\x50' | nc CAMERA_IP_ADDRESS 8000 | tail -c +17 | java -jar zmodowned.jar CAMERA_AES_KEY | ffmpeg -re -an -f h264 -i - -c copy -f rtsp rtsp://server_ip:8554/camera
