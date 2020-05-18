[![Build Status](https://travis-ci.org/amrabed/strace-docker.svg?branch=master)](https://travis-ci.org/amrabed/strace-docker)
[![GitHub issues](https://img.shields.io/github/issues/amrabed/strace-docker.svg)](https://github.com/amrabed/strace-docker/issues)
[![GitHub (pre-)release](https://img.shields.io/github/release/amrabed/strace-docker/all.svg)](https://github.com/amrabed/strace-docker/releases)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

# strace-docker
Trace system calls from Docker containers running on the system<a href="#footnote" id="ref"><sup>*</sup></a>


## Usage
### Install
    git clone https://github.com/amrabed/strace-docker && sudo ./strace-docker/install
    
To check if `strace-docker` is successfully installed and running, use `service strace-docker status`

### Tracing
`strace-docker` is automatically triggered by [`docker events`](https://docs.docker.com/engine/reference/commandline/events) to monitor any new Docker container. The resulting trace of system calls is written to a new file at `/var/log/strace-docker/`. File name will be `$id-$image-$timestamp` where `$id` is the container ID, `$image` is the container image, and `$timestamp` is the time the container started. You can see full log of monitored containers at `/var/log/strace-docker/log`.

[![How to use the strace-docker tool](https://img.youtube.com/vi/iWywV_4Y34E/0.jpg)](https://www.youtube.com/watch?v=iWywV_4Y34E)


## Known Issues
- `strace-docker` does not currently stop tracing process automatically when container is stopped.
- `strace-docker` does not resume tracing to the same file on container restart.
- `strace-docker` relies internally on [`Sysdig`](https://sysdig.com) which limits the number of monitoring processes to 5 by default. Due to `strace-docker` not killing/stopping monitoring processes automatically, `strace-docker` stops montioring new containers when 5 containrs are currently monitored. The user then needs to manually stop any `strace-docker` processes that are no longer needed (i.e., whose containers are not running anymore).

All contributions are welcome :)


<a id="footnote" href="#ref"><sup>*</sup></a> Implemented as part of my Ph.D. dissertation research. See [this paper](https://arxiv.org/abs/1611.03056) for more details
