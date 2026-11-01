# Guide presentation assets

The current guide is generated as complete static pages with normal chapter and
heading links. It reuses Dataverse's Bootstrap 3.4.1 stylesheet at
/resources/bs/css/bootstrap.min.css and requires no guide JavaScript runtime.

The supplied translated diagrams are served locally from ../pics/. Remaining
illustrations retain their supplied official Dataverse 6.6 documentation URLs.
The original chapter HTML fragments remain editable under customization/guide;
build-guide.py wraps them in the shared template and converts their original
HTMX link attributes into ordinary page links.
