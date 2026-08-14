import mysql.connector

try:
    mydb = mysql.connector.connect(
      host="localhost",
      user="root",
      password="tiger"
    )
    mycursor = mydb.cursor()
    mycursor.execute("SELECT DISTINCT user_type FROM multimedia_governance.user_details;")
    for x in mycursor.fetchall():
        print(x)
except Exception as e:
    print(e)
