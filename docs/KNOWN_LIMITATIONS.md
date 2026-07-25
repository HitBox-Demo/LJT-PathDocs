# Known Limitations

The application is ready for demo-mode acceptance testing. These items remain environment-specific or future enhancements:

- The remote Oracle Database connection and SQL transactions have not yet been executed against the target office database.
- Due dates use calendar days; public holidays and working-day calendars are not included.
- Notifications are in-app only; email, SMS and native push delivery are not included.
- Image selection does not include cropping, perspective correction, OCR or drag-and-drop page reordering.
- Source images are converted into the main PDF and are not retained as individually editable PDF pages after submission.
- Browser file-picker behaviour differs by phone and browser. Reopening **Choose Images** adds another selection batch even when the device picker permits only one image at a time.
- Formal organisation-specific printable reports are not yet defined.
- Production deployment still requires HTTPS, secure cookies, backup/retention rules, malware scanning, filesystem permissions and a security review.
- Final concurrency, large-file and long-duration tests must be performed on the real Tomcat and Oracle environment.
