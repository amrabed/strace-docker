# strace-docker
[![Build Status](https://travis-ci.org/amrabed/strace-docker.svg?branch=master)](https://travis-ci.org/amrabed/strace-docker)

Trace system calls from Docker containers running on the system


## Usage
### Install
    git clone https://github.com/amrabed/strace-docker && sudo ./strace-docker/install
    
To check if `strace-docker` is successfully installed and running, use `service strace-docker status`

### Tracing
`strace-docker` is automatically triggered by [`docker events`](https://docs.docker.com/engine/reference/commandline/events) to monitor any new Docker container. The resulting trace of system calls is written to a new file at `/var/log/strace-docker/`. File name will be `$id-$image-$timestamp` where `$id` is the container ID, `$image` is the container image, and `$timestamp` is the time the container started. You can see full log of monitored containers at `/var/log/strace-docker/log`.

## Known Issues
- `strace-docker` does not currently stop tracing process automatically when container is stopped.
- `strace-docker` does not resume tracing to the same file on container restart.
