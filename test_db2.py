import mysql.connector

try:
    mydb = mysql.connector.connect(
      host="localhost",
      user="root",
      password="tiger"
    )
    mycursor = mydb.cursor()
    print("--- EMPLOYEES BY USER_ID ---")
    mycursor.execute("SELECT user_id, count(*) FROM multimedia_governance.employee GROUP BY user_id HAVING count(*) > 1;")
    res = mycursor.fetchall()
    print("Duplicates:", res)
    
except Exception as e:
    print(e)
