shopt -s extglob
rm -rf !(*.sh|*.py|Makefile)
shopt -u extglob

echo "Everything is clean..."