import threading

def log(formatted_str):
    thread_name = threading.current_thread().name
    print("[{:}]: {:}".format(thread_name,formatted_str))