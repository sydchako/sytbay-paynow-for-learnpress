# Sytbay Android v0.3.0

Cache-first Android client for Sytbay Academy.

Bottom tabs: Articles/Home, Learn, Downloads, Institutions, Offline.

The drawer adds Programmes, About Us, Mission & Vision, public website, Privacy Policy, Cookie Policy, Terms & Conditions, Refund & Return, Disclaimer and Settings.

Switching tabs uses SQLite feed cache and does not refetch when a cache exists. Pull-to-refresh is the explicit network refresh. Learning pages are also cache-first: once opened they load locally until the reader refresh button is tapped.

Opened learning pages are stored in SQLite and embedded HTTPS images are copied into app-private storage.

Document links (PDF, DOC/DOCX, XLS/XLSX, PPT/PPTX, ZIP, EPUB, CSV, TXT) can be downloaded through Android DownloadManager. Wi-Fi-only downloads are configurable.

MathJax 3 TeX/SVG rendering is integrated in the reader and can be toggled in Settings. WebView uses cache-else-network for the MathJax script.

AdMob uses Google Mobile Ads SDK 25.5.0 with UMP 4.0.0. Development defaults to Google's official test IDs. Replace ADMOB_APP_ID and ADMOB_BANNER_ID in gradle.properties before monetised release.

Package: zw.co.sytbay.app
Version: 0.3.0
