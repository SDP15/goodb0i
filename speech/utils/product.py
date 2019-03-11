class Product:
    def __init__(self, id, quantity, name, price):
        self.id = id
        self. quantity = quantity
        self.name = name
        self.price = price

    def get_id(self):
        return self.id

    def get_quantity(self):
        return self.quantity
    
    def set_quantity(self, quantity):
        self.quantity = quantity
    
    def get_name(self):
        return self.name

    def get_price(self):
        return self.price
