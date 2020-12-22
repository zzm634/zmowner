# Zmowner

This is an attempt to reverse-engineer the protocol used by Zmodo IP cameras in order to make use of the video and audio feeds. Currently, it is extremely barebones, and won't work without some extra know-how. Upcoming features include:

- Audio streaming
- Framerate detection
- Packaging multiple video and audio streams

## Disclaimer

Zmodo probably won't like this, and will attempt to further obfuscate their video streams in new firmwares. Do not update your camera firmware.

## Tested devices

- Zmodo Ding doorbell (now called "Greet")

## Usage

The java executable `zmowner.jar` is used to decrypt streams. The program can either connect directly to a camera by IP, parse a Zmodo ".264" file, or accept a raw stream from stdin. At the moment, only a single stream of video output is produced as a raw h.264 stream, with no codec information or audio. Most media players cannot decode this. This stream must be passed through to something else (such as 'ffmpeg' or 'vlc') to be converted to a playable video stream or file.

### Conversion of Zmodo ".264" files

The AES key of the video is usually kept in the video header, so you do not have to provide the key as an argument.

    java -jar zmowner.jar --file 162021_162326.264 | ffmpeg -f h264 -i - -c copy 162021_162326.mp4
    
or

    cat 162021_162326.264 | java -jar zmowner.jar | ffmpeg -f h264 -i - -c copy 162021_162326.mp4
    
### Integration with DVR software (Agent DVR, ZoneMinder, etc)

Since this program just outputs the stream, you will need an intermediary server to receive the stream and re-transmit it to clients. I used https://github.com/aler9/rtsp-simple-server for this. Make sure to leave `zmowner` running in the background (i.e., with  `screen`)

    java -jar zmowner.jar -h 192.168.0.123 | ffmpeg -re -f h264 -i - -c copy -f rtsp rtsp://rtsp-simple-server-host:8554/camera_name
    
Then you may configure your DVR to connect to the RTSP server at `rtsp://rtsp-simple-server-host:8554/camera_name`.

Change "rtsp-simple-server-host" to the host name or IP address of your server (like "127.0.0.1"), and "camera_name" can be any string to identify the camera (such as "doorbell").
