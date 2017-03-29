FROM sysdig/sysdig
LABEL maintainer "AmrAbed@vt.edu"

# Install Prerequisites
RUN apt-get update && \
    apt-get install -y curl gawk inotify-tools

# Install Sysdig
#RUN  curl -s https://s3.amazonaws.com/download.draios.com/stable/install-sysdig | bash

# Install strace-docker
COPY strace-docker /usr/bin/strace-docker
COPY etc/init.d/strace-docker /etc/init.d/strace-docker
RUN mkdir /var/log/strace-docker && \
    update-rc.d strace-docker defaults

VOLUME /var/log/strace-docker
VOLUME /var/lib/docker

CMD service strace-docker start && \
    touch /var/log/strace-docker/log && \
    tail -f /var/log/strace-docker/log


