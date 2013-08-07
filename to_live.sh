#!/bin/sh

scp target/narmame-standalone.jar narma.me:/srv/apps/narma.me/
ssh narma.me 'sudo systemctl restart narmame'

