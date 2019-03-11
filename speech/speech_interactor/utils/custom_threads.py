import threading

class WorkerThread(threading.Thread):
    def __init__(self, name, object_instance, work_queue, event_flag=None):
        threading.Thread.__init__(self, name=name)
        self.obj_instance = object_instance
        self.work_queue = work_queue
        self.event_flag = event_flag

    def run(self):
        while True:
            items = self.work_queue.get()
            args = ""
            if type(items) == tuple:
                func = items[0]
                args = items[1:]
            else:
                func = items

            function_to_call = getattr(self.obj_instance, func)

            if len(args) > 0:
                function_to_call(*args)
            else:
                function_to_call()

            if self.event_flag:
                self.event_flag.set()
