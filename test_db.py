import mysql.connector

try:
    mydb = mysql.connector.connect(
      host="localhost",
      user="root",
      password="tiger"
    )
    mycursor = mydb.cursor()
    print("--- SUPER ADMINS ---")
    mycursor.execute("SELECT super_admin_id, email FROM multimedia_governance.super_admin;")
    for x in mycursor.fetchall():
        print(x)
        
    print("--- USER DETAILS ---")
    mycursor.execute("SELECT user_id, email, super_admin_id FROM multimedia_governance.user_details;")
    for x in mycursor.fetchall():
        print(x)
except Exception as e:
    print(e)
