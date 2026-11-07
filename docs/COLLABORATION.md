# Genuine two-person workflow

Do not backdate commits or use another person's identity. This project does not manufacture a Git history. Each student should understand, test, improve and commit their own work. If working alone, obtain the teacher's permission and follow the teacher's two-account rule.

Suggested responsibilities (proposal, NOT a record of work already performed):
- Member A: theme/footer, MNB service and chart, calculator and JUnit tests.
- Member B: OANDA client/forms, React/Axios CRUD and H2 persistence.
- Both: review, integration tests, packaging, screenshots and documentation.

Create a GitHub Project with Todo / In progress / Review / Done. Open and assign issues for each component. Link each issue to a branch and pull request; the other student reviews it.

At least five real milestones:
1. Java 25 skeleton, Maven setup, landing page and footer.
2. SOAP request/parser, date validation and Chart.js integration.
3. OANDA account, instrument selection, prices and candles.
4. OANDA orders, positions, trade closing and validation.
5. Calculator tests, React CRUD and persistent H2 database.
6. Integration fixes, real screenshots, documentation and root homework.jar.

For each milestone record actual author, issue URL, commit SHA, PR URL and reviewer in the PDF. Do not split an already-finished upload merely to pretend incremental collaboration. Use the supplied components to conduct real staged implementation/review work and confirm acceptable use of assistance with your teacher.

On each member's own machine:
```
git config user.name "Your actual full name"
git config user.email "Your verified GitHub email"
git clone YOUR_PUBLIC_REPOSITORY_URL
git switch -c feature/your-assigned-task
# implement, understand, test and review your assigned changes
git add SELECTED_FILES
git commit -m "Describe the actual completed work"
git push -u origin feature/your-assigned-task
```
Then open a pull request, request partner review, fix feedback and merge. Keep the final JAR at root and verify its download in a fresh folder with Java 25.
