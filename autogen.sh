echo "Generating build information ..."
aclocal
autoheader
automake --include-deps
autoconf
echo "Please run ./configure now."

