g++ -I header -c src/Personagens.cpp -o object/Personagens.o
g++ -I header -c src/Player.cpp -o object/Player.o
g++ -I header -c src/Monster.cpp -o object/Monster.o
g++ -I header -c main.cpp -o object/main.o
g++ object/*.o -o RPG
