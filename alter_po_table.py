import mysql.connector

try:
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="tiger",
        database="multimedia_governance"
    )
    cursor = conn.cursor()
    cursor.execute("ALTER TABLE portal_purchase_orders MODIFY pr_id BIGINT NULL;")
    cursor.execute("ALTER TABLE portal_purchase_orders MODIFY quotation_id BIGINT NULL;")
    cursor.execute("ALTER TABLE portal_purchase_orders MODIFY vendor_id BIGINT NULL;")
    conn.commit()
    print("Successfully altered table to make pr_id, quotation_id, and vendor_id nullable.")
except Exception as e:
    print(f"Error: {e}")
finally:
    if 'conn' in locals() and conn.is_connected():
        cursor.close()
        conn.close()
