package com.sdp15.goodb0i.data.store

import com.google.gson.Gson

object TestDataItemLoader : ItemLoader {

    private val items: MutableCollection<Item> = arrayListOf()

    private val json = """[

      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "L",
        "name": "British Whole Milk 3.408L, 6 Pints",
        "UnitOfSale": 1,
        "description": ["Pasteurised standardised homogenised whole milk.", "From British Farms. Supporting our trusted dairy farmers Fair For Farmers Guarantee: We Promise every farmer is paid fairly for every pint of milk, every pint is 100% British and every cow is well cared for For more information go to www.tescoplc.com/little helps plan/"],
        "AverageSellingUnitWeight": 2.083,
        "UnitQuantity": "LITRE",
        "id": 1,
        "ContentsQuantity": 3.408,
        "department": "Milk, Butter & Eggs",
        "price": 1.5,
        "unitprice": 0.44
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "G",
        "name": "Grated 30% Reduced Fat Mature Cheese 250G",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 0.256,
        "description": ["Medium fat hard mature cheese.", "Rich & Versatile Hand selected by our cheesemakers for taste"],
        "UnitQuantity": "KG",
        "id": 2,
        "ContentsQuantity": 250,
        "department": "Cheese",
        "price": 1.9,
        "unitprice": 7.6
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "G",
        "name": "Lurpak Unsalted Block Butter 250G",
        "UnitOfSale": 1,
        "description": ["Unsalted Butter."],
        "AverageSellingUnitWeight": 0.263,
        "UnitQuantity": "KG",
        "id": 3,
        "ContentsQuantity": 250,
        "department": "Milk, Butter & Eggs",
        "price": 2.0,
        "unitprice": 8.0
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "G",
        "name": "British Chicken Breast Portions 650G",
        "UnitOfSale": 1,
        "description": ["Fresh Class A skinless chicken breast fillet portions.", "From Trusted Farms Fed on a wholegrain diet for a succulent texture"],
        "AverageSellingUnitWeight": 0.746,
        "UnitQuantity": "KG",
        "id": 4,
        "ContentsQuantity": 650,
        "department": "Fresh Meat & Poultry",
        "price": 3.8,
        "unitprice": 5.85
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "G",
        "name": "8 Pork Loin Steaks 1.08Kg",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 1.26,
        "description": ["Boneless pork loin steaks.", "From trusted farms. Working with selected farmers that we trust, to ensure high welfare standards and consistent quality."],
        "UnitQuantity": "KG",
        "id": 5,
        "ContentsQuantity": 1080,
        "department": "Fresh Meat & Poultry",
        "price": 5.0,
        "unitprice": 4.63
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "G",
        "name": "Boswell Farms Beef Mince 500G 20% Fat",
        "UnitOfSale": 1,
        "description": ["Typical percentage fat content under 20% Typical percentage collagen/ meat protein ratio under 17% For UK Market Only", "BOSWELL FARMS BEEF MINCE 20% FAT"],
        "AverageSellingUnitWeight": 0.591,
        "UnitQuantity": "KG",
        "id": 6,
        "ContentsQuantity": 500,
        "department": "Fresh Meat & Poultry",
        "price": 1.49,
        "unitprice": 2.98
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "G",
        "name": "Sweet Vine Ripened Tomatoes 230G",
        "UnitOfSale": 1,
        "description": ["Sweet vine ripened tomatoes", "Sweet vine ripened tomatoes"],
        "AverageSellingUnitWeight": 0.292,
        "UnitQuantity": "KG",
        "id": 7,
        "ContentsQuantity": 230,
        "department": "Fresh Salad & Dips",
        "price": 1.0,
        "unitprice": 4.35
      },
      {"superDepartment": "Fresh Food",
        "ContentsMeasureType": "SNGL",
        "name": "Bunched Carrots Bag",
        "UnitOfSale": 1,
        "description": ["Crunchy and sweet, perfect for roasting", "Bunched in the field by hand ~ High in Vitamin A"],
        "AverageSellingUnitWeight": 0.733,
        "UnitQuantity": "EACH",
        "id": 8,
        "ContentsQuantity": 1,
        "department": "Fresh Vegetables",
        "price": 1.5,
        "unitprice": 1.5
      },
      {"superDepartment": "Fresh Food",
        "ContentsMeasureType": "G",
        "name": "Broccoli Florets 240G",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 0.305,
        "description": ["Broccoli.", "Crunchy and full of flavour"],
        "UnitQuantity": "KG",
        "id": 9,
        "ContentsQuantity": 240,
        "department": "Fresh Vegetables",
        "price": 1.3,
        "unitprice": 5.42
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "SNGL",
        "name": "Tesco Organic Small Bananas 6 Pack",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 0.891,
        "UnitQuantity": "EACH",
        "id": 10,
        "ContentsQuantity": 6,
        "department": "Fresh Fruit",
        "price": 1.39,
        "unitprice": 0.232
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "SNGL",
        "name": "Jaffa Red Oranges Minimum 5 Pack",
        "UnitOfSale": 1,
        "description": ["Oranges.", "Jaffa Reds/Pink"],
        "AverageSellingUnitWeight": 0.933,
        "UnitQuantity": "EACH",
        "id": 11,
        "ContentsQuantity": 5,
        "department": "Fresh Fruit",
        "price": 2.25,
        "unitprice": 0.45
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "KG",
        "name": "Red Seedless Grapes 500G",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 0.52,
        "description": ["Jack's Red Seedless Grapes", "Jack's Red Seedless Grapes"],
        "UnitQuantity": "KG",
        "id": 12,
        "ContentsQuantity": 0.5,
        "department": "Fresh Fruit",
        "price": 2.0,
        "unitprice": 4.0
      },
      {
        "superDepartment": "Bakery",
        "ContentsMeasureType": "SNGL",
        "name": "New York Bakery Plain Bagels 5 Pack",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 0.462,
        "description": ["5 Plain Bagels", "For all the latest news and recipe ideas join our Facebook community at www.facebook.com/newyorkbakery"],
        "UnitQuantity": "EACH",
        "id": 13,
        "ContentsQuantity": 5,
        "department": "Bread & Rolls",
        "price": 1.6,
        "unitprice": 0.32
      },
      {
        "superDepartment": "Bakery",
        "ContentsMeasureType": "G",
        "name": "Tesco Stay Fresh White Medium Bread 800G",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 121.367,
        "description": ["Medium sliced white bread.", "Baked to stay fresh for longer"],
        "UnitQuantity": "100G",
        "id": 14,
        "ContentsQuantity": 800,
        "department": "Bread & Rolls",
        "price": 0.7,
        "unitprice": 0.088
      },
      {
        "superDepartment": "Bakery",
        "ContentsMeasureType": "SNGL",
        "name": "Weetabix Apricot And Oat Breakfast Muffin 4 Pack",
        "UnitOfSale": 1,
        "description": ["4 Muffins with dried apricots, oats and poppy seeds and topped with wholegrain wheat flakes"],
        "AverageSellingUnitWeight": 0.631,
        "UnitQuantity": "EACH",
        "id": 15,
        "ContentsQuantity": 4,
        "department": "Doughnuts, Cookies & Muffins",
        "price": 1.8,
        "unitprice": 0.45
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "G",
        "name": "Tesco Smoked Salmon 240G",
        "UnitOfSale": 1,
        "description": ["Smoked salmon (Salmo salar) slices, defrosted.", "READY TO EAT Smoked Salmon Slices. Gently smoked with oak and beechwood for a delicately mild flavour. Our Smoked Salmon is farmed in waters off the coast of Norway or Scotland. Cured to lock in succulence, then gently kiln smoked using smouldering oak for robustness and beechwood for a subtle sweetness. Finally it is left to mature to allow the flavour to develop before slicing."],
        "AverageSellingUnitWeight": 0.247,
        "UnitQuantity": "100G",
        "id": 16,
        "ContentsQuantity": 240,
        "department": "Chilled Fish & Seafood",
        "price": 6.4,
        "unitprice": 2.67
      },
      {
        "superDepartment": "Frozen Food",
        "ContentsMeasureType": "KG",
        "name": "4 Breaded Cod Fillets 500G",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 0.546,
        "description": ["4 Skinless and boneless cod (Gadus morhua) fillets, formed from pieces of 100% Cod fillet coated in breadcrumbs.", "Formed from 100% cod fillet coated in crispy breadcrumbs"],
        "UnitQuantity": "KG",
        "id": 17,
        "ContentsQuantity": 0.5,
        "department": "Frozen Fish & Seafood",
        "price": 2.75,
        "unitprice": 5.5
      },
      {
        "superDepartment": "Fresh Food",
        "ContentsMeasureType": "G",
        "name": "Finest Cooked Jumbo King Prawns 150G",
        "UnitOfSale": 1,
        "description": ["Cooked and peeled king prawns (Litopenaeus vannamei), defrosted.", "Tesco finest* Jumbo King Prawns Juicy and plump warm water prawns. Gently cooked to enhance their naturally sweet flavour. Why not try: Adding to a risotto with peas, finish with Parmesan and fresh parsley. Responsibly sourcing our seafood is important to us, which is why Tesco fish experts work with responsibly managed farms and fisheries to continually improve their high standards of quality, welfare and sustainability."],
        "AverageSellingUnitWeight": 0.2,
        "UnitQuantity": "KG",
        "id": 18,
        "ContentsQuantity": 150,
        "department": "Chilled Fish & Seafood",
        "price": 3.75,
        "unitprice": 25.0
      },
      {
        "superDepartment": "Food Cupboard",
        "ContentsMeasureType": "G",
        "name": "Kelloggs All Bran Cereal 500G",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 0.466,
        "description": ["High-fibre wheat bran cereal", "Have a look at our websites for lots of tasty recipes", "Visit our website for delicious recipes & information www.allbran.co.uk", "Enjoy as part of a varied and balanced diet and a healthy lifestyle."],
        "UnitQuantity": "100G",
        "id": 19,
        "ContentsQuantity": 500,
        "department": "Cereals",
        "price": 2.4,
        "unitprice": 0.48
      },
      {
        "superDepartment": "Food Cupboard",
        "ContentsMeasureType": "G",
        "name": "Everyday Value Tomato Ketchup 550G",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 0.599,
        "description": ["Tomato ketchup.", "Tasted and approved by customers"],
        "UnitQuantity": "100G",
        "id": 20,
        "ContentsQuantity": 550,
        "department": "Table Sauces, Marinades & Dressings",
        "price": 0.42,
        "unitprice": 0.076
      },

      {
        "image": "http://img.tesco.com/Groceries/pi/402/5000184592402/IDShot_90x90.jpg",
        "superDepartment": "Food Cupboard",
        "ContentsMeasureType": "G",
        "name": "Napolina Fusilli Pasta 500G",
        "UnitOfSale": 1,
        "description": ["Fusilli"],
        "AverageSellingUnitWeight": 0.54,
        "UnitQuantity": "KG",
        "id": 21,
        "ContentsQuantity": 500,
        "department": "Dried Pasta, Rice, Noodles & Cous Cous",
        "price": 0.64,
        "unitprice": 1.28
      },
      {
        "superDepartment": "Food Cupboard",
        "ContentsMeasureType": "G",
        "name": "Galaxy Chocolate Multipack 4 X42g",
        "UnitOfSale": 1,
        "AverageSellingUnitWeight": 0.203,
        "description": ["Milk chocolate"],
        "UnitQuantity": "100G",
        "id": 22,
        "ContentsQuantity": 168,
        "department": "Chocolate",
        "price": 1.5,
        "unitprice": 0.893
      },
      {
        "superDepartment": "Food Cupboard",
        "ContentsMeasureType": "G",
        "name": "Haribo Jelly Babies 180G",
        "UnitOfSale": 1,
        "description": ["Fruit Flavour Gums", "Visit our website www.haribo.com"],
        "AverageSellingUnitWeight": 0.216,
        "UnitQuantity": "100G",
        "id": 23,
        "ContentsQuantity": 180,
        "department": "Sweets, Mints & Chewing Gum",
        "price": 1.0,
        "unitprice": 0.556
      },
      {
        "superDepartment": "Food Cupboard",
        "ContentsMeasureType": "G",
        "name": "Metcalfes Skinny Cinema Sweet Popcorn 70 G",
        "UnitOfSale": 1,
        "description": ["Cinema sweet style popped corn with sugar and sweeteners.", "Tweet me before you eat me", "#popitlikeitsmetcalfes"],
        "AverageSellingUnitWeight": 0.076,
        "UnitQuantity": "100G",
        "id": 24,
        "ContentsQuantity": 75,
        "department": "Crisps, Snacks & Popcorn",
        "price": 1.6,
        "unitprice": 2.14
      }
   ]"""

    init {
        val gson = Gson()
        items.addAll(gson.fromJson(json, Array<Item>::class.java))
    }

    override suspend fun loadItem(id: Long): Item {
        return items.find { it.id == id }!!
    }

    override suspend fun loadCategory(category: String): PaginatedResult<Item> {
        return PaginatedResult(items.filter { it.department == category }, 0, false)
    }

    override suspend fun search(query: String): PaginatedResult<Item> {
        return PaginatedResult(items.filter {
            (it.name + it.department + it.description).toLowerCase().contains(query.toLowerCase())
        }, 0, false)
    }

    override suspend fun loadAll(): List<Item> {
        return items.toList()
    }
}