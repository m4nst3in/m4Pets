# M4Pets - Plugin de Pets para Minecraft 1.21.5

## Descrição
M4Pets é um plugin completo de pets para servidores Minecraft em português. Os jogadores podem comprar, melhorar e personalizar diferentes tipos de pets, principalmente montarias.

## Requisitos
- Servidor Paper 1.21.5
- Plugin DecentHolograms
- Java 21

## Funcionalidades
- Sistema de pets com 4 categorias: Guerreiros, Montarias, Trabalhadores e Decoração
- Pets de montaria totalmente funcionais (Porco, Cavalo, Égua, Burro, Ovelha, Vaca, Sniffer)
- Sistema de níveis (1-5) com melhorias de velocidade e vida
- Habilidades especiais desbloqueadas no nível 5
- Sistema de cosméticos (partículas, cores)
- Holograma mostrando informações do pet (nome, vida, nível)
- Armazenamento em SQLite com HikariCP
- Interface gráfica intuitiva
- Totalmente configurável

## Comandos
- `/pets` - Abre o menu principal de pets
- `/pets <nome>` - Spawna um pet específico

## Permissões
- `m4pets.use` - Permissão para usar o comando /pets (padrão: true)
- `m4pets.admin` - Permissão para comandos administrativos (padrão: op)

## Instalação
1. Baixe o plugin DecentHolograms e coloque-o na pasta plugins do seu servidor
2. Baixe M4Pets e coloque-o na pasta plugins do seu servidor
3. Reinicie o servidor
4. Configure o plugin em config.yml conforme necessário

## Configuração
Todas as configurações estão disponíveis no arquivo `config.yml` que será gerado após a primeira execução do plugin. Você pode:
- Alterar preços dos pets
- Adicionar novos tipos de pets
- Configurar as estatísticas base e melhorias
- Personalizar mensagens
- Ajustar configurações de GUI
- E muito mais!

## Desenvolvimento Futuro
As categorias de pets em desenvolvimento incluem:
- Guerreiros (pets que lutam ao lado do jogador)
- Trabalhadores (pets que ajudam em tarefas como mineração)
- Decoração (pets puramente estéticos)

## Créditos
Desenvolvido por m4nst3in