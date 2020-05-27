import argparse
import os
import pymysql

HOST = 'localhost'
USERNAME = 'root'
PASSWORD = 'password'
DATABASE = 'ObserveRTC'
CHARSET = 'utf8mb4'

parser = argparse.ArgumentParser(description="Setup the database")
parser.add_argument("version", help="The version of the database we want to run",
                    choices=['1.0.0'])
parser.add_argument("-u", "--username", help="username for the database", default=USERNAME)
parser.add_argument("-p", "--password", help="password for the database", default=PASSWORD)
parser.add_argument("-s", "--server", help="host of the database", default=HOST)
parser.add_argument("-c", "--charset", help="used charset for the database", default=CHARSET)
parser.add_argument("-d", "--database", help="the database name", default=DATABASE)
parser.add_argument("-o", "--offset",
                    help="set an offset in the version, which executes script from the filename defined as offset",
                    default=None)
parser.add_argument("-b", "--breakpoint",
                    help="set a break point in the version, which stops theeexecution of the script at the filename defined as a break point",
                    default=None)

args = parser.parse_args()
from pymysql.constants import CLIENT

connection = pymysql.connect(host=args.server,
                             user=args.username,
                             password=args.password,
                             # db=args.database,
                             charset=args.charset,
                             cursorclass=pymysql.cursors.DictCursor,
                             client_flag=CLIENT.MULTI_STATEMENTS)


def main():
    connection.cursor().execute('CREATE DATABASE IF NOT EXISTS ObserveRTC')
    connection.cursor().execute('USE ObserveRTC')
    print(args.version)
    files = os.listdir(args.version)  # list of directory files
    files.sort()
    offset_active = args.offset is not None
    break_active = args.breakpoint is not None
    for filename in files:
        print("Open file", filename)
        if offset_active:
            if filename != args.offset:
                print("Skip this file, due to offset param")
                continue
            offset_active = False

        if break_active:
            if filename == args.breakpoint:
                print("Program reached breakpoint, we stop execution")
                break_active = False
                break

        with open("/".join([args.version, filename]), 'r') as file:
            sql_script = file.read()
            print("Content of ", filename, "has been read")
            with connection.cursor() as cursor:
                print("Executing scripts: ", sql_script)
                cursor.execute(sql_script)
    connection.commit()
    return 0


if __name__ == "__main__":
    main()
