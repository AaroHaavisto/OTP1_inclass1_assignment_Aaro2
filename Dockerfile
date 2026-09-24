FROM ubuntu:latest
LABEL authors="aaroo"

ENTRYPOINT ["top", "-b"]