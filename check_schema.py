import mysql.connector

try:
    mydb = mysql.connector.connect(
      host="localhost",
      user="root",
      password="tiger",
      database="multimedia_governance"
    )
    cursor = mydb.cursor()
    cursor.execute("DESCRIBE user_details")
    print(cursor.fetchall())
except Exception as e:
    print(e)
