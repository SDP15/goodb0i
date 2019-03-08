# import the necessary packages
from pyzbar import pyzbar
import cv2
import imutils



def show_webcam(mirror=False):
    # initialize the camera and grab a reference to the raw camera capture
    # cam = VideoStream(src=0).start()
    cam = cv2.VideoCapture(0)
    while True:
        ret_val, img = cam.read()
        if mirror:
            img = cv2.flip(img, 1)
            cv2.imshow("Live video capture", img)
        cv2.imwrite("frame.jpg", frame)
        data = detect_qrcodes("frame.jpg")
        print("Data inside QR code: %s" %data)
        if cv2.waitKey(1) == 27:
            break  # esc to quit
    cv2.destroyAllWindows()


def detect_qrcodes(arg):
    # load the input image
    #image = cv2.imdecode(img, cv2.IMREAD_GRAYSCALE)
    #cv2.imshow("Image", image)
    img = cv2.imread(arg, cv2.IMREAD_GRAYSCALE)
    image = imutils.resize(img, width=400)
    ret, thresh = cv2.threshold(image, 127, 255, cv2.THRESH_BINARY)  # binarise the image
    cv2.imshow("Binarised image in grayscale", thresh)
    # find the barcodes in the image and decode each of the barcodes

    # cv2.imshow("Threshold", thresh)
    # find the barcodes in the image and decode each of the barcodes
    barcodes = pyzbar.decode(thresh)

    # loop over the detected barcodes
    for barcode in barcodes:
        # extract the bounding box location of the barcode and draw the
        # bounding box surrounding the barcode on the image
        print("barcode detected")
        (x, y, w, h) = barcode.rect
        cv2.rectangle(thresh, (x, y), (x + w, y + h), (0, 0, 255), 2)

        # the barcode data is a bytes object so if we want to draw it on
        # our output image we need to convert it to a string first
        barcodeData = barcode.data.decode("utf-8")
        barcodeType = barcode.type
        print(barcodeData)
        # draw the barcode data and barcode type on the image
        text = "{} ({})".format(barcodeData, barcodeType)

        return barcodeData


def main():
    show_webcam(mirror=True)

if __name__ == "__main__":
    main()