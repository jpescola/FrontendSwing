package view;

import config.AppConfig;
import model.CategoriaDTO;
import model.ProdutoDTO;
import service.CategoriaService;
import service.ProdutoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProdutoView extends JFrame {
    private JTextField txtNome;
    private JTextField txtPreco;
    private JComboBox cmbCategoria;
    private JButton btnAddCategoria;
    private JButton btnManual;
    private JButton btnVisualizar;
    private JButton btnNovo;
    private JButton btnExcluir;
    private JButton btnSalvar;
    private JTable tblDados;
    private JLabel lblFoto;
    private JPanel principal;

    private java.util.List<ProdutoDTO> listaProdutos = new ArrayList<>();
    private List<CategoriaDTO> categorias;
    private String tokenJwt;
    private ProdutoDTO produtoSelecionado;
    private File arquivoFotoSelecionado = null;
    private File arquivoManualSelecionado = null;

    public ProdutoView(String tokenJwt) {
        this.tokenJwt = tokenJwt;
        setContentPane(principal);
        setTitle("Gerenciamento de Produtos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // adiciona uma borda no rótulo
        lblFoto.setBorder(BorderFactory.createLineBorder(Color.black));
        setSize(1300, 1000);

        // definindo os ícones dos botões
        btnSalvar.setIcon(new ImageIcon(ProdutoView.class.getResource("/icons/Check_16x16.png")));
        btnExcluir.setIcon(new ImageIcon(ProdutoView.class.getResource("/icons/Delete_16x16.png")));
        btnNovo.setIcon(new ImageIcon(ProdutoView.class.getResource("/icons/New_16x16.png")));
        btnManual.setIcon(new ImageIcon(ProdutoView.class.getResource("/icons/Text Document_16x16.png")));
        btnVisualizar.setIcon(new ImageIcon(ProdutoView.class.getResource("/icons/Preview_16x16.png")));
        btnAddCategoria.setIcon(new ImageIcon(ProdutoView.class.getResource("/icons/Add_16x16.png")));

        // personalizações
        lblFoto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tblDados.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // buscando dados
        carregarCategoriasNoCombo();
        carregarTabelaProdutos();

        btnSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    try {
                        ProdutoDTO dto = coletarDadosDoFormulario();
                        ProdutoService service = new ProdutoService();
                        String retorno = service.salvarProduto(dto,
                                arquivoFotoSelecionado, arquivoManualSelecionado, tokenJwt);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(getParent(), retorno);
                            limpar();
                            btnSalvar.setEnabled(true);
                            carregarTabelaProdutos();
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(getParent(),
                                    "Erro ao enviar: " + ex.getMessage(),
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            }
        });

        btnNovo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpar();
                btnNovo.setEnabled(true);
                btnSalvar.setEnabled(true);
            }
        });

        tblDados.getSelectionModel().addListSelectionListener(event -> {
            int linhaSelecionada = tblDados.getSelectedRow();
            if (event.getValueIsAdjusting() || linhaSelecionada == -1) {
                return;
            }
            produtoSelecionado = listaProdutos.get(linhaSelecionada);
            txtNome.setText(produtoSelecionado.getNome() != null ? produtoSelecionado.getNome() : "");
            txtPreco.setText(String.format(java.util.Locale.US, "%.2f", produtoSelecionado.getPreco()));

            String nomeCategoriaProduto = produtoSelecionado.getNomeCategoria();
            if (nomeCategoriaProduto != null && cmbCategoria != null) {
                for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                    Object item = cmbCategoria.getItemAt(i);
                    if (item instanceof CategoriaDTO) {
                        CategoriaDTO cat = (CategoriaDTO) item;
                        if (cat.getNome().equalsIgnoreCase(nomeCategoriaProduto)) {
                            cmbCategoria.setSelectedIndex(i);
                            break;
                        }
                    } else if (item != null && item.toString().equalsIgnoreCase(nomeCategoriaProduto)) {
                        cmbCategoria.setSelectedIndex(i);
                        break;
                    }
                }
            }

            lblFoto.setIcon(null);
            lblFoto.setText("Foto");
            if (produtoSelecionado.getFoto() != null) {
                exibirFotoDoServidor(produtoSelecionado.getFoto());
            }

            btnVisualizar.setEnabled(false);
            if (produtoSelecionado.getManual() != null) {
                btnVisualizar.setEnabled(true);
            }
            btnNovo.setEnabled(true);
            btnExcluir.setEnabled(true);
            btnSalvar.setEnabled(true);
        });

        btnAddCategoria.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nomeCategoria = javax.swing.JOptionPane.showInputDialog(
                        null,
                        "Digite o nome da nova categoria:",
                        "Nova Categoria",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (nomeCategoria != null && !nomeCategoria.trim().isEmpty()) {
                    try {
                        CategoriaService categoriaService = new CategoriaService();
                        categoriaService.salvarCategoria(nomeCategoria.trim(), tokenJwt);
                        javax.swing.JOptionPane.showMessageDialog(null,
                                "Categoria cadastrada com sucesso!");
                        carregarCategoriasNoCombo();
                    } catch (Exception ex) {
                        javax.swing.JOptionPane.showMessageDialog(
                                null,
                                "Erro ao salvar categoria: " + ex.getMessage(),
                                "Erro",
                                javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                        ex.printStackTrace();
                    }
                }
            }
        });

        btnExcluir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirmacao = JOptionPane.showConfirmDialog(
                        getParent(),
                        "Tem certeza que deseja excluir o produto "
                                + produtoSelecionado.getNome() + "?",
                        "Confirmação de Exclusão",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmacao == JOptionPane.YES_OPTION) {
                    new Thread(() -> {
                        try {
                            ProdutoService service = new ProdutoService();
                            service.excluirProduto(produtoSelecionado.getId(), tokenJwt);
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(getParent(),
                                        "Produto excluído com sucesso!");
                                carregarTabelaProdutos(); // Atualiza a JTable
                                limpar();
                                btnNovo.setEnabled(false);
                                btnExcluir.setEnabled(false);
                                btnVisualizar.setEnabled(false);
                            });
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(getParent(),
                                        "Erro ao excluir: " + ex.getMessage(),
                                        "Erro", JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }).start();
                }
            }
        });

        lblFoto.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Selecionar Foto do Produto");
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Imagens (JPG, PNG)", "jpg", "jpeg", "png"
                ));
                int resultado = fileChooser.showOpenDialog(getParent());
                if (resultado == JFileChooser.APPROVE_OPTION) {
                    arquivoFotoSelecionado = fileChooser.getSelectedFile();
                    exibirFotoLocal(arquivoFotoSelecionado.getAbsolutePath());
                }
            }
        });

        btnManual.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Selecionar Manual (PDF)");
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Documentos PDF", "pdf"
                ));
                int resultado = fileChooser.showOpenDialog(getParent());
                if (resultado == JFileChooser.APPROVE_OPTION) {
                    arquivoManualSelecionado = fileChooser.getSelectedFile();
                }
            }
        });

        btnVisualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirManualPdfDoServidor(produtoSelecionado.getManual());
            }
        });
    }

    private void carregarCategoriasNoCombo() {
        new Thread(() -> {
            CategoriaService service = new CategoriaService();
            categorias = service.listarCategorias(tokenJwt);
            SwingUtilities.invokeLater(() -> {
                cmbCategoria.removeAllItems(); // Limpa itens anteriores
                for (CategoriaDTO cat : categorias) {
                    cmbCategoria.addItem(cat); // O JComboBox usa o toString() para exibir o nome
                }
            });
        }).start();
    }

    public void carregarTabelaProdutos() {
        new Thread(() -> {
            ProdutoService service = new ProdutoService();
            listaProdutos = service.listarProdutos(tokenJwt);
            SwingUtilities.invokeLater(() -> {
                String[] colunas = {"ID", "Nome", "Preço"};
                DefaultTableModel model = new DefaultTableModel(colunas, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                for (ProdutoDTO p : listaProdutos) {
                    Object[] linha = {
                            p.getId(),
                            p.getNome(),
                            String.format("R$ %.2f", p.getPreco())
                    };
                    model.addRow(linha);
                }
                tblDados.setModel(model);
            });
        }).start();
    }

    public ProdutoDTO coletarDadosDoFormulario() {
        ProdutoDTO dto = new ProdutoDTO();

        if (produtoSelecionado != null) {
            dto.setId(produtoSelecionado.getId());
        }

        dto.setNome(txtNome.getText());

        try {
            dto.setPreco(Double.valueOf(txtPreco.getText().replace(",", ".")));
        } catch (NumberFormatException e) {
            dto.setPreco(0.0);
        }

        CategoriaDTO categoriaSelecionada = (CategoriaDTO) cmbCategoria.getSelectedItem();
        if (categoriaSelecionada != null) {
            dto.setCategoria(categoriaSelecionada); // Envia apenas o ID para a API
        }

        if (arquivoFotoSelecionado != null) {
            dto.setFoto(arquivoFotoSelecionado.getName());
        }
        if (arquivoManualSelecionado != null) {
            dto.setManual(arquivoManualSelecionado.getName());
        }
        return dto;
    }

    private void limpar() {
        produtoSelecionado = null;
        arquivoFotoSelecionado = null;
        arquivoManualSelecionado = null;

        txtNome.setText("");
        txtPreco.setText("");
        cmbCategoria.setSelectedIndex(0);
        lblFoto.setText("Foto");
        lblFoto.setIcon(null);
        tblDados.clearSelection();
        btnVisualizar.setEnabled(false);
        btnNovo.setEnabled(false);
        btnExcluir.setEnabled(false);
        btnSalvar.setEnabled(false);

        txtNome.requestFocus();
    }

    private void exibirFotoDoServidor(String nomeArquivoFoto) {
        String urlFoto = AppConfig.getApiUrl() + nomeArquivoFoto;
        try {
            ImageIcon imagemOriginal = new ImageIcon(new java.net.URL(urlFoto));
            Image imagemRedimensionada = imagemOriginal
                    .getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            lblFoto.setIcon(new ImageIcon(imagemRedimensionada));
            lblFoto.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirManualPdfDoServidor(String nomeArquivoManual) {
        String urlPdf = AppConfig.getApiUrl() + nomeArquivoManual;
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.browse(new java.net.URI(urlPdf));
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro ao abrir o manual: " + e.getMessage(),
                    "Erro", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exibirFotoLocal(String caminhoArquivo) {
        if (caminhoArquivo != null && !caminhoArquivo.isEmpty()) {
            try {
                ImageIcon imagemOriginal = new ImageIcon(caminhoArquivo);
                if (imagemOriginal.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE && imagemOriginal.getIconWidth() != -1) {
                    Image imagemRedimensionada = imagemOriginal.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    lblFoto.setIcon(new ImageIcon(imagemRedimensionada));
                    lblFoto.setText("");
                } else {
                    lblFoto.setIcon(null);
                    lblFoto.setText("Arquivo inválido");
                }
            } catch (Exception e) {
                e.printStackTrace();
                lblFoto.setIcon(null);
                lblFoto.setText("Erro ao carregar");
            }
        } else {
            lblFoto.setIcon(null);
            lblFoto.setText("Sem foto");
        }
    }
}
