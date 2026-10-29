FreeRails documentation
=======================

The documentation pages are written in Markdown and built with Sphinx using
MyST. Keep Markdown source wrapped to roughly 80-100 characters so it remains
easy to read in editors and diff views.

Install the documentation dependencies with:

    python -m pip install -r requirements.txt

Build the HTML documentation from this directory with:

    python -m sphinx -M html . _build

The project dictionary is in the dictionary folder; see its README there.