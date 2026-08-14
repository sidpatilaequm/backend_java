import mysql.connector

try:
    mydb = mysql.connector.connect(
      host="localhost",
      user="root",
      password="tiger",
      database="multimedia_governance"
    )
    cursor = mydb.cursor()
    cursor.execute("SELECT user_id, user_type FROM user_details WHERE user_type = 'approver' OR user_type = 'APPROVER'")
    print(cursor.fetchall())
except Exception as e:
    print(e)
