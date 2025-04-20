## 🛠️ Setup Instructions

1. **Install Java JDK 11+**
2. **Install MySQL & MySQL Workbench**
3. Import the `db_file_to_import/harshitChoubeyDB.sql` using MySQL Workbench.
4. Open the project in VS Code.
5. Ensure `mysql-connector.jar` is in `lib/` and referenced in `.vscode/settings.json`.
6. Update `db.properties` with your DB credentials.
7. Run `src/loginandsignup/OopsProj.java`.

## To Import harshitChoubeyDB.sql into ur system using MySQL Workbench

1. Open MySQL Workbench
2. Connect to your database
3. Create a New Database (Schema)
   If you haven’t already:
   In the Navigator > SCHEMAS panel (left-hand side), right-click and choose:
   Create Schema
   Name it something like: `your_db`
   Click Apply → Apply again → Finish
4. Go to File > Open SQL Script
   Top Menu → Click File → Open SQL Script
   Browse to your `harshitChoubeyDB.sql` file
   Click Open – It will load the SQL file into the query editor
5. Select the Target Database
   Before running the script:
   In the Schemas panel (left), right-click your `your_db` schema → Set as Default Schema
   (This ensures tables will be created inside this DB)
6. Run the Script
   Click the lightning bolt icon (⚡) or press Ctrl+Shift+Enter
   Wait until the script finishes running — all your tables/data should now be imported.
7. Refresh the Schema
   In the SCHEMAS panel:
   Right-click your `your_db` → Click Refresh All
   You should now see your tables!
