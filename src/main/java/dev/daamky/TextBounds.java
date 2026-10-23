package dev.daamky;

public record TextBounds(float x, float y, float width, float height) {
    public float maxX() {
        return this.x + this.width;
    }

    public float maxY() {
        return this.y + this.height;
    }

    public float centerX() {
        return this.x + this.width * 0.5F;
    }

    public float centerY() {
        return this.y + this.height * 0.5F;
    }

    public boolean contains(float pointX, float pointY) {
        return pointX >= this.x && pointY >= this.y && pointX < maxX() && pointY < maxY();
    }
}
