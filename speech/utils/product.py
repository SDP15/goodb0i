class Product:
    def __init__(self, id, quantity, name, price, shelf_number=None, shelf_position=None):
        self.id = id
        self.quantity = quantity
        self.name = name
        self.price = price
        self.shelf_number = shelf_number
        self.shelf_position = shelf_position

    def get_id(self):
        return self.id

    def get_quantity(self):
        return self.quantity
    
    def get_name(self):
        return self.name

    def get_price(self):
        return self.price

    def get_shelf_number(self):
        return self.shelf_number

    def get_shelf_position(self):
        return self.shelf_position

    def set_id(self):
        return self.id

    def set_quantity(self):
        return self.quantity
    
    def set_name(self):
        return self.name

    def set_price(self):
        return self.price

    def set_shelf_number(self):
        return self.shelf_number

    def set_shelf_position(self):
        return self.shelf_position