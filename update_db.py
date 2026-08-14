import mysql.connector

try:
    mydb = mysql.connector.connect(
      host="localhost",
      user="root",
      password="tiger"
    )
    mycursor = mydb.cursor()
    mycursor.execute("UPDATE multimedia_governance.user_details SET user_type = 'APPROVER' WHERE user_type = 'approver';")
    mydb.commit()
    print("Database updated!")
except Exception as e:
    print(e)
