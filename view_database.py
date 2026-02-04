import sqlite3

conn = sqlite3.connect('data/atm_database.db')
cursor = conn.cursor()

print('\n' + '='*80)
print('TECHNICIAN CREDENTIALS')
print('='*80)
cursor.execute('SELECT id, username, password, full_name, role, created_date, last_login FROM technician_credentials')
credentials = cursor.fetchall()
if credentials:
    for row in credentials:
        last_login = row[6] if row[6] else "Never"
        print(f'ID: {row[0]} | Username: {row[1]:15s} | Password: {row[2]:10s} | Name: {row[3]:25s} | Role: {row[4]:10s} | Created: {row[5]} | Last Login: {last_login}')
else:
    print("No technician credentials found.")

print('\n' + '='*80)
print('ACCOUNTS TABLE - All Customer Account Information')
print('='*80)
cursor.execute('SELECT * FROM accounts')
for row in cursor.fetchall():
    print(f'Account #: {row[0]:6s} | Name: {row[1]:15s} | Balance: ${row[2]:10,.2f} | PIN: {row[3]}')

print('\n' + '='*80)
print('ATM STATE - Machine Resources')
print('='*80)
cursor.execute('SELECT * FROM atm_state')
for row in cursor.fetchall():
    print(f'ATM Cash: ${row[1]:10,.2f} | Paper Sheets: {row[2]:3d} | Ink Units: {row[3]:3d}')

print('\n' + '='*80)
print('TRANSACTIONS LOG - All Transactions (Last 20)')
print('='*80)
cursor.execute('SELECT id, account_number, account_holder, transaction_type, amount, previous_balance, new_balance, timestamp FROM transactions ORDER BY id DESC LIMIT 20')
transactions = cursor.fetchall()
if transactions:
    for row in transactions:
        print(f'ID: {row[0]:3d} | Account: {row[1]:6s} | Holder: {row[2]:15s} | Type: {row[3]:15s} | Amount: ${row[4]:8,.2f} | Prev: ${row[5]:8,.2f} | New: ${row[6]:8,.2f} | Time: {row[7]}')
else:
    print("No transactions found.")

print('\n' + '='*80)
print('TECHNICIAN ACTIVITIES LOG - All Technician Actions (Last 20)')
print('='*80)
cursor.execute('SELECT id, activity_type, amount, description, previous_value, new_value, timestamp FROM technician_activities ORDER BY id DESC LIMIT 20')
activities = cursor.fetchall()
if activities:
    for row in activities:
        amount_str = f'${row[2]:.2f}' if row[2] is not None else 'N/A'
        prev_str = f'{row[4]:.2f}' if row[4] is not None else 'N/A'
        new_str = f'{row[5]:.2f}' if row[5] is not None else 'N/A'
        print(f'ID: {row[0]:3d} | Type: {row[1]:15s} | Amount: {amount_str:>10} | Description: {row[3]:25s} | Prev: {prev_str:>10} | New: {new_str:>10} | Time: {row[6]}')
else:
    print("No technician activities found.")

conn.close()
