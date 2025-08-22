#include <iostream>
#define MAX_SIZE 100
using std::cout

int add(int a, int b){
    return a+b;
}

int main() {
    int x = 5;
    int y = 10;
    int result = add(x, y);

    int arr[MAX_SIZE];
    for (int i = 0; i < 3; i++) {
        arr[i] = i*i;
    }
    switch(result) {
        case 10:
            cout << "Ten\\n";
            break;
        case 15:
            cout << "Fifteen\\n";
            break;
        default:
            cout << "Other\\n";

    }

    return 0;


}
