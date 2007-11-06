#!/bin/bash
# Deploys the website
# This script is remotely called by the deploysite ant target.
cd /home/groups/f/fr/freerails2/
rm htdocs/* -d -r -f
gunzip website.tar.gz
mv website.tar htdocs/
cd htdocs/
tar -xf website.tar
rm website.tar