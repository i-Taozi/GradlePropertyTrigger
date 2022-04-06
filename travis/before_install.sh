#!/bin/sh

# Set up the database
psql -c 'create database myria_test;' -U postgres

# Set up SSH so that we can SSH to localhost
ssh-keygen -t rsa -N "" -f ~/.ssh/id_rsa_localhost -q
cat ~/.ssh/id_rsa_localhost.pub >> ~/.ssh/authorized_keys
ssh-keyscan -t rsa localhost >> ~/.ssh/known_hosts
echo "IdentityFile ~/id_rsa" >> ~/.ssh/config
echo "IdentityFile ~/id_rsa_localhost" >> ~/.ssh/config

# Set up myria-web
pip install --user paste
pip install --user webapp2
pip install --user webob
pip install --user jinja2
cd ~
git clone https://github.com/uwescience/raco.git
pip install --user --editable ~/raco

git clone https://github.com/uwescience/myria-web.git
cd ~/myria-web
git submodule init
git submodule update
# overwrite raco submodule with symlink to raco repo dir
ln -sf ~/raco ~/myria-web/submodules/raco
python setup.py install

# Set up myria-python
pip install --user myria-python

# Install flake for style check
pip install --user flake8
