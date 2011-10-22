import os
from flask import Flask, request, redirect, url_for, send_from_directory
from werkzeug import secure_filename

UPLOAD_FOLDER = './uploads'
ALLOWED_EXTENSIONS = set(['pdf'])

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# ensuring the file extensions are allowed
def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

@app.route('/')
def index():
    return 'Welcome to ImageToPDF!! <br> Scan your documents with your phone!!'

@app.route('/upload', methods = ['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        file = request.files['file']
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            return redirect(url_for('uploaded_file', filename = filename))
    
    return '''
    <!doctype html>
    <title>Upload File</title>
    <h1> Upload File</h1>
    <form action="" method=post enctype=multipart/form-data>
       <p><input type=file name=file>
          <input type=submit value=Upload>
    </form>
    '''

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

if __name__ == '__main__':
    app.debug = True
    app.run(host = '0.0.0.0', port=5001)
