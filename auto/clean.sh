shopt -s extglob
rm -rf !(*.sh|*.py|Makefile|mars.jar)
shopt -u extglob

echo "Everything is clean..."