from PIL import Image
import os

try:
    # Input paths
    ico_path = "client-javafx/favicon.ico"
    output_path = "client-javafx/src/main/resources/app-icon.png"
    
    # Create directory if not exists
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    # Open and save as PNG
    img = Image.open(ico_path)
    img.save(output_path, "PNG")
    print(f"Successfully converted {ico_path} to {output_path}")
    
except Exception as e:
    print(f"Error converting icon: {e}")
