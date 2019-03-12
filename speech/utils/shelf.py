class Shelf:
    def __init__(self, shelf_id, items_on_shelf):
        self.shelf_id = shelf_id
        self.items_on_shelf = items_on_shelf

    def get_shelf_id(self):
        return self.shelf_id

    def get_items_on_shelf(self):
        return self.items_on_shelf