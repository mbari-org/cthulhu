# Spec: Acceptance Checklist

## Target OS 
- [ ] macOS (Catalina)
- [ ] Linux (Ubuntu 19.10)

## Video Playback

- [ ] Play AV1 4K local file
- [ ] Play AV1 4K remote file via http
- [ ] Play AV1 HD local file
- [ ] Play AV1 HD remote file via http
- [ ] __Play H.264 HD local file__ * 
- [ ] __Play H.264 HD remote file via http__ * 
- [ ] Play HEVC 4K local file
- [ ] Play HEVC 4K remote file via http
- [ ] Play HEVC HD local file
- [ ] Play HEVC HD remote file via http
- [ ] __Play ProRes 422 HD local file__ * 
- [ ] Play VP9 HD remote file via http

'*' Required. Others are strongly desired.

## Video UI Controls

### Play/pause

- [ ] Play/pause button that toogles play/pause state of video playback
- [ ] Play icon is displayed when the video is paused/stopped
- [ ] Pause icon is displayed when video is playing or shuttling forward or shuttling reverse

### Video Scrubber

- [ ] Video scrubber/slider (This is the slider seen on every video player)
- [ ] Scrubber should display current position into video
- [ ] Moving slider seeks to correct position in video
- [ ] When an external seek command is received via UDP, the video will seek to that position and the slider will correctly reflect that position.

### Current Elapsed Time Label

- [ ] Current time label displaying the elapsed time into the video at second resolution in mm:ss format

### Total duration/remaining time label

- [ ] Total/remaining time label 
- [ ] By default, displays the total duration of the video in mm:ss format.
- [ ] When toggled by clicking on the label, will display the remaining duration of the video in mm:ss format
- [ ] Clicking the label toggles between total duration and remaining duration

### Open local file menu item


- [ ] When item to open local file

### Open remote URL menu item

- [ ] Menu item to open remote file (using a URL)

### Menu item to set UDP port

- [ ] Menu item to set a preference for the UDP remote communication port.
- [ ] Changing the port number restarts the process listening for remote UDP requests on the specified port
- [ ] Changing the port number saves the port to a local preference.
- [ ] On startup, this preference is read and the UDP process is started listening on this port.

### Menu item to set time-window for display of an individual bounding box.

- [ ] Menu item to set a preference for the a time-window (in seconds) to display a bounding box.
- [ ] Changing the time-window saves it to a local preference
        
## UDP Remote Control

Commands to be supported as defined in [UDP.md](UDP):

__NOTE:__ MBARI provides a library for the remote communications at [vcr4j-sharktopoda-client](https://github.com/mbari-media-management/vcr4j/tree/master/vcr4j-sharktopoda-client).

- [ ] Connect
- [ ] Open
- [ ] Close
- [ ] Show
- [ ] Request Video Information for a Specific Window 
- [ ] Request information for all open videos
- [ ] Play
- [ ] Pause
- [ ] Request elapsed time 
- [ ] Request status
- [ ] Seek to elapsed time
- [ ] Framecapture
- [ ] Frame advance

## Bounding Box Display

### UI to draw bounding box

- [ ] When a user

### UDP Communication to receive bounding box data

- [ ] Receive array of bounding boxes via UDP. (They will have elapsed time, x, y, width, height, label and a UUID)
- [ ] Receive new bounding boxes via UDP. These will be added to the existing array to be displayed.

### UDP Communication to set _selected_ bounding boxes

- [ ] Receive array of __selected__ bounding box uuids.
- [ ] Selected bounding box will be displayed in a manner (such as a different color) to distinguish them from other bounding boxes.

## Display Bounding boxes

- [ ] At a given elapsed time in the video, display bounding boxes corresponding to that time
- [ ] Bounding box is displayed overlaid on video from `-timewindow /  2` to `+timewindow / 2`
- [ ] Bounding box has some indicator to differentiate when it's at the correct frame (or as near the correct frame as possible).
- [ ] Bounding box should also display it's label anchored at the lower right edge of the box
