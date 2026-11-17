FreeRails documentation
=======================

The user manual pages are written in Markdown and built with Sphinx into HTML using
MyST. The development documents are also written in Markdown but not intended to be
converted. Keep Markdown source wrapped to roughly 80-100 characters so it remains
easy to read in editors and diff views.

Install the documentation dependencies with:

    python -m pip install -r requirements.txt

Build the HTML documentation from the docs/user directory with:

    python -m sphinx -M html . _build

The project dictionary is in the dictionary folder; see its README there.