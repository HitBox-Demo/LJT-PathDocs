# Manual Acceptance Checklist

## Authentication and access

- [ ] Clerk, Boss and each Department User can sign in and sign out.
- [ ] Users cannot open pages outside their assigned roles.
- [ ] Session expiry returns the user to Login.

## Document creation and files

- [ ] Choose one image and see its thumbnail.
- [ ] Open Choose Images again and add another image without losing the first.
- [ ] Select several images in one operation when supported by the device.
- [ ] Remove one selected image using `X`.
- [ ] Submit the remaining images and confirm the PDF page count/order.
- [ ] Upload an existing main PDF instead of images.
- [ ] Upload and download PDF/image/Word/Excel attachments.
- [ ] Unsupported extensions and mismatched file signatures are rejected.

## Workflow

- [ ] Clerk saves a draft and edits it.
- [ ] Clerk submits to the selected boss.
- [ ] Destination and Selected Boss display correctly.
- [ ] Only the selected boss sees the pending item.
- [ ] Clerk recalls a pending item; Boss can no longer approve it.
- [ ] Boss returns a document with a mandatory reason.
- [ ] Clerk edits the same ID/code and resubmits it.
- [ ] Earlier rejection remains in Approval History.
- [ ] Boss changes destination and Clerk is notified.
- [ ] Boss approves; the document becomes `ROUTED`.
- [ ] Correct department sees it; other departments do not.
- [ ] Bulk approval processes only selected pending documents.

## Administration and reports

- [ ] Clerk creates a user.
- [ ] Clerk creates a role-change request.
- [ ] Boss approves/rejects the role request.
- [ ] Approval History, notifications, pending counts and overdue indicators display correctly.
- [ ] Long titles, names, emails and filenames remain inside their containers.
- [ ] Tables remain usable on 320 px, 360 px, tablet and desktop widths.

## PWA and deployment

- [ ] The latest CSS/JavaScript loads after clearing an older service worker.
- [ ] The install prompt appears on a supported HTTPS/localhost deployment.
- [ ] Static assets work offline; authenticated documents are not cached.
